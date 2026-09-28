import type { CpqBomItem } from '../types/cpq'

type BomNode = CpqBomItem & { children: BomNode[] }

type Props = {
  items: CpqBomItem[]
  level?: number
}

function CpqBomTree({ items, level = 0 }: Props) {
  const nodes = items
    .filter((item) => !item.parentItemNumber)
    .map((item) => buildNode(item, items))

  return (
    <div className="bom-tree">
      {nodes.map((item) => (
        <BomNodeView key={item.itemNumber} item={item} level={level} />
      ))}
    </div>
  )
}

function buildNode(item: CpqBomItem, items: CpqBomItem[]): BomNode {
  return {
    ...item,
    children: items
      .filter((child) => child.parentItemNumber === item.itemNumber)
      .map((child) => buildNode(child, items)),
  }
}

function BomNodeView({ item, level }: { item: BomNode; level: number }) {
  return (
    <div>
      <div className="bom-row" style={{ paddingLeft: `${level * 24}px` }}>
        <div>
          <strong>{item.description ?? item.partNumber ?? 'BOM Item'}</strong>
          {item.partNumber && <span>{item.partNumber}</span>}
        </div>
        <span>Qty {item.quantity ?? 1}</span>
      </div>
      {item.children.map((child) => (
        <BomNodeView key={child.itemNumber} item={child} level={level + 1} />
      ))}
    </div>
  )
}

export default CpqBomTree