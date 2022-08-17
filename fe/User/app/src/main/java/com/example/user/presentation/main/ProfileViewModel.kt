package com.example.user.presentation.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.user.domain.usecase.LogOutUseCase
import com.example.user.utils.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val logOutUseCase: LogOutUseCase
): ViewModel() {
    private val _logout = MutableLiveData<Response<Int>>()
    val logout get() = _logout

    fun logout(){
        _logout.postValue(Response.loading(null))
        CoroutineScope(Dispatchers.IO).launch {
            _logout.postValue(logOutUseCase.invoke())
        }
    }
}