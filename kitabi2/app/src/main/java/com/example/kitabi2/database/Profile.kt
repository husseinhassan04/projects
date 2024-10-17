package com.example.kitabi2.database

import java.util.UUID

class Profile{
    var id = UUID.randomUUID().toString()
    var username=""
    var password=""
    var email=""
    var url=""
}
