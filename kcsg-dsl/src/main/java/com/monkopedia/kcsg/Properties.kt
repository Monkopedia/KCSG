package com.monkopedia.kcsg

import eu.mihosoft.jcsg.Cylinder


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