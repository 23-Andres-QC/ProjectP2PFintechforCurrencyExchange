package com.example.p2p.domain.repository

import com.example.p2p.core.network.NetworkResult
import com.example.p2p.data.remote.model.Complaint
import com.example.p2p.data.remote.model.ComplaintsResponse
import com.example.p2p.data.remote.model.CreateComplaintRequest

interface ComplaintRepository {
    suspend fun getMyComplaints(page: Int = 1, perPage: Int = 20): NetworkResult<ComplaintsResponse>
    suspend fun createComplaint(request: CreateComplaintRequest): NetworkResult<Complaint>
}