package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.Building
import com.example.myentrance.domain.repository.BuildingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BuildingRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : BuildingRepository {

    override suspend fun getBuildingById(buildingId: String): Building? {
        val doc = firestore.collection("buildings").document(buildingId).get().await()
        return if (doc.exists()) {
            Building(
                id = buildingId,
                address = doc.getString("address") ?: "",
                buildingName = doc.getString("buildingName") ?: "",
                adminId = doc.getString("adminId") ?: ""
            )
        } else null
    }

    override suspend fun getBuildingsForUser(userId: String): List<Building> {
        val snap = firestore.collection("buildings")
            .whereEqualTo("adminId", userId)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            getBuildingById(doc.id)
        }
    }

    override suspend fun addBuilding(building: Building): Boolean {
        val data = mapOf(
            "address" to building.address,
            "buildingName" to building.buildingName,
            "adminId" to building.adminId
        )
        firestore.collection("buildings").document(building.id).set(data).await()
        return true
    }
}