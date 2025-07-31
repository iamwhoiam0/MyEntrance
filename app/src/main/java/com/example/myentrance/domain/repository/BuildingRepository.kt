package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.Building

interface BuildingRepository {
    suspend fun getBuildingById(buildingId: String): Building?
    suspend fun getBuildingsForUser(userId: String): List<Building>
    suspend fun addBuilding(building: Building): Boolean
}