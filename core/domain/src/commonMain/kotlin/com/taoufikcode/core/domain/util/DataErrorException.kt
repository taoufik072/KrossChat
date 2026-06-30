package com.taoufikcode.core.domain.util

class DataErrorException(
    val error: DataError
): Exception()