// Roadmap layout engine. Pure, framework-free, and unit-tested. Turns a
// node/edge graph into an ordered list of render rows reproducing the
// roadmap.sh-style flowchart: a central spine of core/milestone nodes with
// optional topics branching off to alternating sides.

export interface RoadmapNodeInput {
  nodeKey: string;
  type: string; // "topic" | "milestone"
  title: string;
  summary?: string | null;
  detail?: string | null;
  optional: boolean;
  orderIndex: number;
  courseId?: string | null;
  courseTitle?: string | null;
}

export interface RoadmapEdgeInput {
  fromNodeKey: string;
  toNodeKey: string;
  kind: string; // "sequence" | "branch" | "related"
  style: string; // "solid" | "dotted"
}

export type NodeRole = "milestone" | "core" | "optional";

export interface LayoutItem {
  node: RoadmapNodeInput;
  role: NodeRole;
  side?: "left" | "right"; // only for optional branches
}

export interface RoadmapLayout {
  items: LayoutItem[]; // in render (top-to-bottom) order
  spineKeys: string[]; // core + milestone nodes, in spine order
}

export interface TopoResult {
  order: string[];
  hasCycle: boolean;
}

/**
 * Kahn's algorithm. Ties among ready nodes are broken by orderIndex so the
 * output is deterministic and, for the seeded chains, matches authoring order.
 * Edges referencing unknown nodes or self-loops are ignored. When a cycle
 * exists, {@code order} is shorter than the node count and hasCycle is true.
 */
export function topoSort(nodes: RoadmapNodeInput[], edges: RoadmapEdgeInput[]): TopoResult {
  const keys = new Set(nodes.map((n) => n.nodeKey));
  const indeg = new Map<string, number>();
  const adj = new Map<string, string[]>();
  for (const n of nodes) {
    indeg.set(n.nodeKey, 0);
    adj.set(n.nodeKey, []);
  }
  for (const e of edges) {
    if (!keys.has(e.fromNodeKey) || !keys.has(e.toNodeKey) || e.fromNodeKey === e.toNodeKey) {
      continue;
    }
    adj.get(e.fromNodeKey)!.push(e.toNodeKey);
    indeg.set(e.toNodeKey, (indeg.get(e.toNodeKey) ?? 0) + 1);
  }

  const orderIdx = new Map(nodes.map((n) => [n.nodeKey, n.orderIndex]));
  const ready = nodes.filter((n) => (indeg.get(n.nodeKey) ?? 0) === 0).map((n) => n.nodeKey);
  const order: string[] = [];

  while (ready.length > 0) {
    ready.sort((a, b) => (orderIdx.get(a) ?? 0) - (orderIdx.get(b) ?? 0));
    const key = ready.shift()!;
    order.push(key);
    for (const next of adj.get(key)!) {
      const d = (indeg.get(next) ?? 0) - 1;
      indeg.set(next, d);
      if (d === 0) ready.push(next);
    }
  }

  return { order, hasCycle: order.length !== nodes.length };
}

/**
 * Builds the render layout. Nodes are placed in topological order (falling back
 * to orderIndex when the graph has a cycle or is disconnected from the sort).
 * Optional nodes alternate left/right; milestones render as centered notes;
 * everything else sits on the central spine.
 */
export function layoutRoadmap(
  nodes: RoadmapNodeInput[],
  edges: RoadmapEdgeInput[],
): RoadmapLayout {
  const byKey = new Map(nodes.map((n) => [n.nodeKey, n]));
  const { order, hasCycle } = topoSort(nodes, edges);

  const orderedKeys =
    !hasCycle && order.length === nodes.length
      ? order
      : [...nodes].sort((a, b) => a.orderIndex - b.orderIndex).map((n) => n.nodeKey);

  let optionalCount = 0;
  const items: LayoutItem[] = orderedKeys.map((key) => {
    const node = byKey.get(key)!;
    if (node.type === "milestone") {
      return { node, role: "milestone" };
    }
    if (node.optional) {
      const side: "left" | "right" = optionalCount++ % 2 === 0 ? "left" : "right";
      return { node, role: "optional", side };
    }
    return { node, role: "core" };
  });

  const spineKeys = items.filter((i) => i.role !== "optional").map((i) => i.node.nodeKey);
  return { items, spineKeys };
}
