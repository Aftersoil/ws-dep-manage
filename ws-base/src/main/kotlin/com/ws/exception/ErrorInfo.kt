package com.ws.exception

interface ErrorInfo {

    fun getResultCode(): String?

    fun getResultMsg(): String?

}