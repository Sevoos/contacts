import type {CategoryNodeObject} from "@/utils/CategoryNodeObject.ts";

export type TreeNavItem =
  | {
  type: "node"
  key: string
  depth: number
}
  | {
  type: "contact"
  id: bigint
  parentKey: string
  depth: number
}

export type ContactItemIdentifier = {
  type: "contact";
  contactId: bigint
}

export type NodeItemIdentifier = {
  type: "node";
  key: string
}

export function flattenVisibleTree(
  node: CategoryNodeObject,
  expandedNodes: Set<string>,
  depth = 0,
  result: TreeNavItem[] = []
): TreeNavItem[] {
  const nodeKey = keyToString(node.key)

  if (depth > 0) {
    result.push({
      type: "node",
      key: nodeKey,
      depth
    })

    if (!expandedNodes.has(nodeKey)) {
      return result
    }
  }

  for (const child of node.children) {
    flattenVisibleTree(child, expandedNodes, depth + 1, result)
  }

  for (const c of node.contacts) {
    result.push({
      type: "contact",
      id: c.id,
      parentKey: nodeKey,
      depth: depth + 1
    })
  }

  return result
}

export const keyToStringSeparator = "$"

export function keyToString(key: string[]): string {
  return key.join(keyToStringSeparator)
}

export function keyFromString(stringId: string): string[] {
  return stringId.split(keyToStringSeparator)
}
