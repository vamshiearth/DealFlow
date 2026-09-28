export type CpqQuote = {
  quoteNumber: string
  customerName: string
  totalPrice: number
  discount: number
  margin: number
  status?: string
}

export type CpqConfiguration = {
  productFamily?: string
  model?: string
  attributes?: Record<string, string>
}

export type CpqPricing = {
  listPrice?: number
  discountPercentage?: number
  discountAmount?: number
  netPrice?: number
  marginPercentage?: number
}

export type CpqQuoteLine = {
  lineNumber?: number
  partNumber?: string
  description?: string
  quantity?: number
  unitListPrice?: number
  unitNetPrice?: number
  extendedNetPrice?: number
}

export type CpqBomItem = {
  itemNumber?: string
  parentItemNumber?: string | null
  partNumber?: string
  description?: string
  quantity?: number
}

export type CpqQuoteDetail = {
  quoteNumber: string
  customerName: string
  status?: string
  configuration?: CpqConfiguration
  pricing?: CpqPricing
  quoteLines: CpqQuoteLine[]
  bom: CpqBomItem[]
}
