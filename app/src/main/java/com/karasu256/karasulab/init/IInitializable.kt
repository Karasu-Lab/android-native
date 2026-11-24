package com.karasu256.karasulab.init

interface IInitializable : ISteppable {
    suspend fun init()

    fun getInfo(): StepInfo = getStepInfo()
}
