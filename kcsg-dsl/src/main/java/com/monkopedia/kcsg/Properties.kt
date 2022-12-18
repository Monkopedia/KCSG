package com.monkopedia.kcsg


var Cylinder.radius: Double
    get() {
        require(startRadius == endRadius) {
            "Cylinder is uneven, cannot get single radius"
        }
        return startRadius
    }
    set(value) {
        startRadius = value
        endRadius = value
    }