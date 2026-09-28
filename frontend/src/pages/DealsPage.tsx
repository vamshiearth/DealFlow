import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'

import { useNavigate } from 'react-router-dom'

import { getCpqQuote } from '../api/cpqApi'
import { createDeal, getDeals, importDealFromCpq } from '../api/dealApi'
import { useAuth } from '../auth/AuthContext'
import type { CpqQuote } from '../types/cpq'
import type { Deal, DealStatus } from '../types/deal'
import {
  formatCurrency,
  formatDealStatus,
  formatPercentage,
  getDealStatusClass,
} from '../utils/formatters'

const dealStatuses: DealStatus[] = [
  'DRAFT',
  'SUBMITTED',
  'MANAGER_REVIEW',
  'FINANCE_REVIEW',
  'VP_REVIEW',
  'APPROVED',
  'REJECTED',
  'CHANGES_REQUESTED',
  'CANCELLED',
]

function DealsPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [deals, setDeals] = useState<Deal[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState<DealStatus | 'ALL'>('ALL')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState<string | null>(null)
  const [quoteNumber, setQuoteNumber] = useState('')
  const [customerName, setCustomerName] = useState('')
  const [dealValue, setDealValue] = useState('')
  const [discountPercentage, setDiscountPercentage] = useState('')
  const [marginPercentage, setMarginPercentage] = useState('')
  const [showCpqImport, setShowCpqImport] = useState(false)
  const [cpqQuoteNumber, setCpqQuoteNumber] = useState('')
  const [cpqQuote, setCpqQuote] = useState<CpqQuote | null>(null)
  const [cpqLoading, setCpqLoading] = useState(false)
  const [cpqImporting, setCpqImporting] = useState(false)
  const [cpqError, setCpqError] = useState<string | null>(null)

  function resetCreateForm() {
    setQuoteNumber('')
    setCustomerName('')
    setDealValue('')
    setDiscountPercentage('')
    setMarginPercentage('')
    setCreateError(null)
  }

  function resetCpqImport() {
    setCpqQuoteNumber('')
    setCpqQuote(null)
    setCpqError(null)
  }

  async function handleCreateDeal(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    try {
      setCreating(true)
      setCreateError(null)

      const createdDeal = await createDeal({
        quoteNumber: quoteNumber.trim(),
        customerName: customerName.trim(),
        dealValue: Number(dealValue),
        discountPercentage: Number(discountPercentage),
        marginPercentage: Number(marginPercentage),
      })

      setDeals((current) => [createdDeal, ...current])
      resetCreateForm()
      setShowCreateForm(false)
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Unable to create deal.')
    } finally {
      setCreating(false)
    }
  }

  async function handleCpqLookup() {
    const quoteNumber = cpqQuoteNumber.trim()

    if (!quoteNumber) {
      return
    }

    try {
      setCpqLoading(true)
      setCpqError(null)
      setCpqQuote(null)
      setCpqQuote(await getCpqQuote(quoteNumber))
    } catch (err) {
      setCpqError(err instanceof Error ? err.message : 'Unable to load CPQ quote.')
    } finally {
      setCpqLoading(false)
    }
  }

  async function handleCpqImport() {
    if (!cpqQuote) {
      return
    }

    try {
      setCpqImporting(true)
      setCpqError(null)
      const importedDeal = await importDealFromCpq(cpqQuote.quoteNumber)
      setDeals((current) => [importedDeal, ...current])
      resetCpqImport()
      setShowCpqImport(false)
    } catch (err) {
      setCpqError(err instanceof Error ? err.message : 'Unable to import CPQ quote.')
    } finally {
      setCpqImporting(false)
    }
  }

  useEffect(() => {
    async function loadDeals() {
      try {
        setLoading(true)
        setError(null)
        setDeals(await getDeals())
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load deals.')
      } finally {
        setLoading(false)
      }
    }

    void loadDeals()
  }, [])

  const canCreateDeal = Boolean(
    user?.roles.includes('SALES_REP') || user?.roles.includes('CPQ_ADMIN'),
  )
  const canImportCpq = canCreateDeal

  const filteredDeals = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase()

    return [...deals]
      .filter((deal) => {
        const matchesSearch =
          normalizedSearch.length === 0 ||
          deal.quoteNumber.toLowerCase().includes(normalizedSearch) ||
          deal.customerName.toLowerCase().includes(normalizedSearch)
        const matchesStatus =
          statusFilter === 'ALL' || deal.status === statusFilter

        return matchesSearch && matchesStatus
      })
      .sort((a, b) => {
        const aTime = a.updatedAt ?? a.createdAt
        const bTime = b.updatedAt ?? b.createdAt

        if (aTime && bTime) {
          return new Date(bTime).getTime() - new Date(aTime).getTime()
        }

        return b.id - a.id
      })
  }, [deals, search, statusFilter])

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Deals</h2>
          <p>Manage DealFlow opportunities and approval workflows.</p>
        </div>
        {canCreateDeal && (
          <div className="page-actions">
            {canImportCpq && (
              <button
                type="button"
                className="secondary-button"
                onClick={() => {
                  resetCpqImport()
                  setShowCpqImport(true)
                }}
              >
                Import from CPQ
              </button>
            )}
            <button
              type="button"
              className="primary-button"
              onClick={() => {
                resetCreateForm()
                setShowCreateForm(true)
              }}
            >
              + New Deal
            </button>
          </div>
        )}
      </div>

      <div className="deal-toolbar">
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search quote or customer..."
          aria-label="Search deals"
        />
        <select
          value={statusFilter}
          onChange={(event) =>
            setStatusFilter(event.target.value as DealStatus | 'ALL')
          }
          aria-label="Filter deals by status"
        >
          <option value="ALL">All statuses</option>
          {dealStatuses.map((status) => (
            <option key={status} value={status}>
              {formatDealStatus(status)}
            </option>
          ))}
        </select>
      </div>

      {loading && <div className="content-state">Loading deals...</div>}
      {error && <div className="page-error">{error}</div>}

      {!loading && !error && filteredDeals.length === 0 && (
        <div className="empty-state">No deals match your current search or filter.</div>
      )}

      {!loading && !error && filteredDeals.length > 0 && (
        <div className="table-card">
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Quote</th>
                  <th>Customer</th>
                  <th>Value</th>
                  <th>Discount</th>
                  <th>Margin</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {filteredDeals.map((deal) => (
                  <tr
                    key={deal.id}
                    className="clickable-row"
                    onClick={() => navigate(`/deals/${deal.id}`)}
                  >
                    <td><strong>{deal.quoteNumber}</strong></td>
                    <td>{deal.customerName}</td>
                    <td>{formatCurrency(deal.dealValue)}</td>
                    <td>{formatPercentage(deal.discountPercentage)}</td>
                    <td>{formatPercentage(deal.marginPercentage)}</td>
                    <td>
                      <span className={`status-badge status-${getDealStatusClass(deal.status)}`}>
                        {formatDealStatus(deal.status)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showCreateForm && (
        <div className="modal-backdrop">
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="create-deal-title"
          >
            <div className="modal-header">
              <div>
                <h3 id="create-deal-title">Create Deal</h3>
                <p>Create a new draft DealFlow opportunity.</p>
              </div>
              <button
                type="button"
                className="icon-button"
                onClick={() => setShowCreateForm(false)}
                aria-label="Close"
              >
                ×
              </button>
            </div>

            <form className="deal-form" onSubmit={handleCreateDeal}>
              <div className="form-field">
                <label htmlFor="quoteNumber">Quote Number</label>
                <input
                  id="quoteNumber"
                  value={quoteNumber}
                  onChange={(event) => setQuoteNumber(event.target.value)}
                  placeholder="Q-20001"
                  required
                />
              </div>

              <div className="form-field">
                <label htmlFor="customerName">Customer Name</label>
                <input
                  id="customerName"
                  value={customerName}
                  onChange={(event) => setCustomerName(event.target.value)}
                  placeholder="Acme Corporation"
                  required
                />
              </div>

              <div className="form-grid">
                <div className="form-field">
                  <label htmlFor="dealValue">Deal Value</label>
                  <input
                    id="dealValue"
                    type="number"
                    min="0"
                    step="0.01"
                    value={dealValue}
                    onChange={(event) => setDealValue(event.target.value)}
                    placeholder="250000"
                    required
                  />
                </div>
                <div className="form-field">
                  <label htmlFor="discount">Discount %</label>
                  <input
                    id="discount"
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    value={discountPercentage}
                    onChange={(event) => setDiscountPercentage(event.target.value)}
                    placeholder="15"
                    required
                  />
                </div>
                <div className="form-field">
                  <label htmlFor="margin">Margin %</label>
                  <input
                    id="margin"
                    type="number"
                    min="0"
                    max="100"
                    step="0.01"
                    value={marginPercentage}
                    onChange={(event) => setMarginPercentage(event.target.value)}
                    placeholder="20"
                    required
                  />
                </div>
              </div>

              {createError && <div className="page-error">{createError}</div>}

              <div className="modal-actions">
                <button
                  type="button"
                  className="secondary-button"
                  disabled={creating}
                  onClick={() => setShowCreateForm(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="primary-button" disabled={creating}>
                  {creating ? 'Creating...' : 'Create Deal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showCpqImport && (
        <div className="modal-backdrop">
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cpq-import-title"
          >
            <div className="modal-header">
              <div>
                <h3 id="cpq-import-title">Import from Oracle CPQ</h3>
                <p>Look up a CPQ quote before importing it into DealFlow.</p>
              </div>
              <button
                type="button"
                className="icon-button"
                aria-label="Close"
                onClick={() => {
                  resetCpqImport()
                  setShowCpqImport(false)
                }}
              >
                ×
              </button>
            </div>

            <div className="deal-form">
              <div className="form-field">
                <label htmlFor="cpqQuoteNumber">Quote Number</label>
                <div className="lookup-row">
                  <input
                    id="cpqQuoteNumber"
                    value={cpqQuoteNumber}
                    onChange={(event) => setCpqQuoteNumber(event.target.value)}
                    placeholder="Q-10005"
                  />
                  <button
                    type="button"
                    className="secondary-button"
                    disabled={cpqLoading || !cpqQuoteNumber.trim()}
                    onClick={() => void handleCpqLookup()}
                  >
                    {cpqLoading ? 'Looking up...' : 'Look up'}
                  </button>
                </div>
              </div>

              {cpqError && <div className="page-error">{cpqError}</div>}

              {cpqQuote && (
                <div className="cpq-preview">
                  <div><span>Quote</span><strong>{cpqQuote.quoteNumber}</strong></div>
                  <div><span>Customer</span><strong>{cpqQuote.customerName}</strong></div>
                  <div><span>Deal Value</span><strong>{formatCurrency(cpqQuote.totalPrice)}</strong></div>
                  <div><span>Discount</span><strong>{formatPercentage(cpqQuote.discount)}</strong></div>
                  <div><span>Margin</span><strong>{formatPercentage(cpqQuote.margin)}</strong></div>
                </div>
              )}

              <div className="modal-actions">
                <button
                  type="button"
                  className="secondary-button"
                  disabled={cpqImporting}
                  onClick={() => {
                    resetCpqImport()
                    setShowCpqImport(false)
                  }}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="primary-button"
                  disabled={!cpqQuote || cpqImporting}
                  onClick={() => void handleCpqImport()}
                >
                  {cpqImporting ? 'Importing...' : 'Import Quote'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default DealsPage
