package com.example.partb;

import java.util.List;

public class ImagesResponse {
    private List<Image> hits;

    public ImagesResponse() {}

    public List<Image> getHits() {
        return hits;
    }

    public void setHits(List<Image> hits) {
        this.hits = hits;
    }
}
