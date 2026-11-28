package com.karasu256.karasulab.init

import com.karasu256.karasulab.R
import com.karasu256.karasulab.data.FirebaseRepository
import com.karasu256.karasulab.logging.Log
import javax.inject.Inject

class FirebaseInitializer @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) : IInitializable {
    override suspend fun init() {
        Log.install(firebaseRepository)
        firebaseRepository.initialize()
    }

    override fun getStepInfo(): StepInfo = StepInfo(
        titleRes = R.string.step_firebase_title,
        descriptionRes = R.string.step_firebase_description,
    )
}
