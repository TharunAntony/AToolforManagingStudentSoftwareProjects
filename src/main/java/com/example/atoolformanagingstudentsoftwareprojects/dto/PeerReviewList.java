package com.example.atoolformanagingstudentsoftwareprojects.dto;

import com.example.atoolformanagingstudentsoftwareprojects.model.PeerReview;

import java.util.List;

public class PeerReviewList {

    private List<PeerReview> peerReviews;

    public PeerReviewList() {}

    public List<PeerReview> getPeerReviews() {
        return peerReviews;
    }

    public void setPeerReviews(List<PeerReview> peerReviews) {
        this.peerReviews = peerReviews;
    }
}
