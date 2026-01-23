package com.example.studylinx.repo

import com.example.studylinx.model.Country
import kotlinx.coroutines.flow.Flow

interface CountryRepo {
    fun observeCountries(): Flow<List<Country>>
    suspend fun addCountry(country: Country)          // create new with push key
    suspend fun upsertCountry(country: Country)       // create/update by id (key)
    suspend fun deleteCountry(countryId: String)
}