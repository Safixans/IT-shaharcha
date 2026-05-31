import { describe, expect, it } from "vitest";
import {
  layoutRoadmap,
  topoSort,
  type RoadmapEdgeInput,
  type RoadmapNodeInput,
} from "./layout";

function node(
  nodeKey: string,
  orderIndex: number,
  opts: Partial<RoadmapNodeInput> = {},
): RoadmapNodeInput {
  return {
    nodeKey,
    type: "topic",
    title: nodeKey,
    summary: null,
    detail: null,
    optional: false,
    orderIndex,
    courseId: null,
    courseTitle: null,
    ...opts,
  };
}

function seq(from: string, to: string): RoadmapEdgeInput {
  return { fromNodeKey: from, toNodeKey: to, kind: "sequence", style: "solid" };
}

function branch(from: string, to: string): RoadmapEdgeInput {
  return { fromNodeKey: from, toNodeKey: to, kind: "branch", style: "dotted" };
}

describe("topoSort", () => {
  it("orders a simple chain", () => {
    const nodes = [node("c", 2), node("a", 0), node("b", 1)];
    const edges = [seq("a", "b"), seq("b", "c")];
    const { order, hasCycle } = topoSort(nodes, edges);
    expect(hasCycle).toBe(false);
    expect(order).toEqual(["a", "b", "c"]);
  });

  it("breaks ties by orderIndex deterministically", () => {
    // a -> b (spine) and a -> opt (branch). opt has lower orderIndex than b.
    const nodes = [node("a", 0), node("opt", 1, { optional: true }), node("b", 2)];
    const edges = [seq("a", "b"), branch("a", "opt")];
    const { order } = topoSort(nodes, edges);
    expect(order).toEqual(["a", "opt", "b"]);
  });

  it("detects a cycle", () => {
    const nodes = [node("a", 0), node("b", 1)];
    const edges = [seq("a", "b"), seq("b", "a")];
    const { order, hasCycle } = topoSort(nodes, edges);
    expect(hasCycle).toBe(true);
    expect(order.length).toBeLessThan(nodes.length);
  });

  it("ignores edges referencing unknown nodes and self-loops", () => {
    const nodes = [node("a", 0), node("b", 1)];
    const edges = [seq("a", "ghost"), seq("a", "a"), seq("a", "b")];
    const { order, hasCycle } = topoSort(nodes, edges);
    expect(hasCycle).toBe(false);
    expect(order).toEqual(["a", "b"]);
  });
});

describe("layoutRoadmap", () => {
  it("classifies core, milestone, and optional roles", () => {
    const nodes = [
      node("a", 0),
      node("opt1", 1, { optional: true }),
      node("b", 2),
      node("ms", 3, { type: "milestone" }),
    ];
    const edges = [seq("a", "b"), seq("b", "ms"), branch("a", "opt1")];
    const { items, spineKeys } = layoutRoadmap(nodes, edges);

    const roleByKey = Object.fromEntries(items.map((i) => [i.node.nodeKey, i.role]));
    expect(roleByKey).toEqual({ a: "core", opt1: "optional", b: "core", ms: "milestone" });
    expect(spineKeys).toEqual(["a", "b", "ms"]); // optionals excluded from spine
  });

  it("alternates optional branch sides left then right", () => {
    const nodes = [
      node("a", 0),
      node("o1", 1, { optional: true }),
      node("b", 2),
      node("o2", 3, { optional: true }),
    ];
    const edges = [seq("a", "b"), branch("a", "o1"), branch("b", "o2")];
    const { items } = layoutRoadmap(nodes, edges);
    const sides = items.filter((i) => i.role === "optional").map((i) => i.side);
    expect(sides).toEqual(["left", "right"]);
  });

  it("falls back to orderIndex ordering when the graph has a cycle", () => {
    const nodes = [node("b", 1), node("a", 0)];
    const edges = [seq("a", "b"), seq("b", "a")];
    const { items } = layoutRoadmap(nodes, edges);
    expect(items.map((i) => i.node.nodeKey)).toEqual(["a", "b"]);
  });
});
