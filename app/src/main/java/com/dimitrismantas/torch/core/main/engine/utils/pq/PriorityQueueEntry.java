package com.dimitrismantas.torch.core.main.engine.utils.pq;

public class PriorityQueueEntry {
    private final int key;
    private final int vertexLabel;

    public PriorityQueueEntry(final int key, final int vertexLabel) {
        this.key = key;
        this.vertexLabel = vertexLabel;
    }

    public int getKey() {
        return key;
    }

    public int getVertexLabel() {
        return vertexLabel;
    }
}
