package com.andiogenes.lox

import java.lang.RuntimeException

class RuntimeError(val token: Token, message: String) : RuntimeException(message)