import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'

import { getCpqQuote, getCpqQuoteDetails } from '../api/cpqApi'
import CpqBomTree from '../components/CpqBomTree'
import { getDeals, importDealFromCpq } from '../api/dealApi'
import { useAuth } from '../auth/AuthContext'
import type { Deal } from '../types/deal'
import type { CpqQuote, CpqQuoteDetail } from '../types/cpq'
import { formatCurrency, formatPercentage, formatStatus } from '../utils/formatters'

function CpqPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [quoteNumber, setQuoteNumber] = useState('')
  const [quote, setQuote] = useState<CpqQuote | null>(null)
  const [quoteDetails, setQuoteDetails] = useState<CpqQuoteDetail | null>(null)
  const [deals, setDeals] = useState<Deal[]>([])
  const [loading, setLoading] = useState(false)
  const [importing, setImporting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const canAccessCpq = Boolean(
    user?.roles.includes('SALES_REP') || user?.roles.includes('CPQ_ADMIN'),
  )

  const importedDeal = useMemo(() => {
    if (!quote) {
      return null
    }

    return deals.find((deal) => deal.quoteNumber === quote.quoteNumber) ?? null
  }, [deals, quote])

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedQuote = quoteNumber.trim()

    if (!normalizedQuote) {
      return
    }

    try {
      setLoading(true)
      setError(null)
      setSuccess(null)
      setQuote(null)
      setQuoteDetails(null)
      const [quoteData, detailData, dealData] = await Promise.all([
        getCpqQuote(normalizedQuote),
        getCpqQuoteDetails(normalizedQuote),
        getDeals(),
      ])
      setQuote(quoteData)
      setQuoteDetails(detailData)
      setDeals(dealData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load CPQ quote.')
    } finally {
      setLoading(false)
    }
  }

  async function handleImport() {
    if (!quote) {
      return
    }

    try {
      setImporting(true)
      setError(null)
      setSuccess(null)
      const imported = await importDealFromCpq(quote.quoteNumber)
      setDeals((current) => [imported, ...current])
      setSuccess('Quote imported into DealFlow successfully.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to import CPQ quote.')
    } finally {
      setImporting(false)
    }
  }

  if (!canAccessCpq) {
    return (
      <section>
        <div className="page-header"><div><h2>Oracle CPQ</h2><p>Search and review Oracle CPQ quote information.</p></div></div>
        <div className="empty-state">Your role does not have access to the CPQ workspace.</div>
      </section>
    )
  }

  return (
    <section>
      <div className="page-header"><div><h2>Oracle CPQ</h2><p>Search Oracle CPQ quotes and bring approved quote data into DealFlow.</p></div></div>

      <form className="cpq-search" onSubmit={handleSearch}>
        <input
          value={quoteNumber}
          onChange={(event) => setQuoteNumber(event.target.value)}
          placeholder="Enter quote number, e.g. Q-10005"
          aria-label="CPQ quote number"
        />
        <button type="submit" className="primary-button" disabled={loading || !quoteNumber.trim()}>
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <div className="page-error cpq-message">{error}</div>}
      {success && <div className="page-success cpq-message">{success}</div>}

      {quote && (
        <>
          <section className="cpq-result-card">
            <div className="cpq-result-header">
              <div><span>Oracle CPQ Quote</span><h3>{quote.quoteNumber}</h3><p>{quote.customerName}</p></div>
              {quote.status && <span className="status-badge status-pending">{formatStatus(quote.status)}</span>}
            </div>
            <div className="detail-grid">
              <article className="detail-card"><span>Total Price</span><strong>{formatCurrency(quote.totalPrice)}</strong></article>
              <article className="detail-card"><span>Discount</span><strong>{formatPercentage(quote.discount)}</strong></article>
              <article className="detail-card"><span>Margin</span><strong>{formatPercentage(quote.margin)}</strong></article>
              <article className="detail-card"><span>DealFlow</span><strong>{importedDeal ? 'Imported' : 'Not imported'}</strong></article>
            </div>
          </section>

          {quoteDetails?.configuration && (
            <section className="cpq-section">
              <div className="section-header"><div><h3>Configuration</h3><p>Product configuration from Oracle CPQ.</p></div></div>
              <div className="cpq-info-grid">
                {quoteDetails.configuration.productFamily && <div><span>Product Family</span><strong>{quoteDetails.configuration.productFamily}</strong></div>}
                {quoteDetails.configuration.model && <div><span>Model</span><strong>{quoteDetails.configuration.model}</strong></div>}
              </div>
              {quoteDetails.configuration.attributes && Object.keys(quoteDetails.configuration.attributes).length > 0 && (
                <div className="cpq-attributes">
                  {Object.entries(quoteDetails.configuration.attributes).map(([name, value]) => <div key={name}><span>{formatStatus(name)}</span><strong>{value}</strong></div>)}
                </div>
              )}
            </section>
          )}

          {quoteDetails?.pricing && (
            <section className="cpq-section">
              <div className="section-header"><div><h3>Pricing</h3><p>Commercial pricing breakdown.</p></div></div>
              <div className="detail-grid">
                {quoteDetails.pricing.listPrice !== undefined && <article className="detail-card"><span>List Price</span><strong>{formatCurrency(quoteDetails.pricing.listPrice)}</strong></article>}
                {quoteDetails.pricing.discountPercentage !== undefined && <article className="detail-card"><span>Discount</span><strong>{formatPercentage(quoteDetails.pricing.discountPercentage)}</strong></article>}
                {quoteDetails.pricing.discountAmount !== undefined && <article className="detail-card"><span>Discount Amount</span><strong>{formatCurrency(quoteDetails.pricing.discountAmount)}</strong></article>}
                {quoteDetails.pricing.netPrice !== undefined && <article className="detail-card"><span>Net Price</span><strong>{formatCurrency(quoteDetails.pricing.netPrice)}</strong></article>}
                {quoteDetails.pricing.marginPercentage !== undefined && <article className="detail-card"><span>Margin</span><strong>{formatPercentage(quoteDetails.pricing.marginPercentage)}</strong></article>}
              </div>
            </section>
          )}

          <section className="cpq-section">
            <div className="section-header"><div><h3>Quote Lines <span className="section-count">{quoteDetails?.quoteLines.length ?? 0}</span></h3><p>Commercial line items in the CPQ transaction.</p></div></div>
            {!quoteDetails?.quoteLines.length ? <div className="empty-state">No quote lines.</div> : <div className="table-container"><table className="data-table"><thead><tr><th>Line</th><th>Product</th><th>Code</th><th>Qty</th><th>Unit List</th><th>Unit Net</th><th>Extended Net</th></tr></thead><tbody>{quoteDetails.quoteLines.map((line, index) => <tr key={`${line.partNumber}-${line.lineNumber ?? index}`}><td>{line.lineNumber ?? index + 1}</td><td>{line.description ?? '—'}</td><td>{line.partNumber ?? '—'}</td><td>{line.quantity ?? '—'}</td><td>{line.unitListPrice !== undefined ? formatCurrency(line.unitListPrice) : '—'}</td><td>{line.unitNetPrice !== undefined ? formatCurrency(line.unitNetPrice) : '—'}</td><td>{line.extendedNetPrice !== undefined ? formatCurrency(line.extendedNetPrice) : '—'}</td></tr>)}</tbody></table></div>}
          </section>

          <section className="cpq-section">
            <div className="section-header"><div><h3>Bill of Materials</h3><p>Hierarchical configured product structure.</p></div></div>
            {quoteDetails?.bom.length ? <CpqBomTree items={quoteDetails.bom} /> : <div className="empty-state">No BOM information.</div>}
          </section>

          <section className="cpq-dealflow-card">
            <div><h3>DealFlow Status</h3>{importedDeal ? <p>This CPQ quote is already connected to DealFlow Deal {importedDeal.id}.</p> : <p>This quote has not yet been imported into DealFlow.</p>}</div>
            {importedDeal ? <button type="button" className="primary-button" onClick={() => navigate(`/deals/${importedDeal.id}`)}>Open Deal</button> : <button type="button" className="primary-button" disabled={importing} onClick={handleImport}>{importing ? 'Importing...' : 'Import into DealFlow'}</button>}
          </section>
        </>
      )}
    </section>
  )
}

export default CpqPage
