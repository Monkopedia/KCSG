$fn = 32;
scenario = "disjoint_union";

module disjoint_union() {
    union() {
        translate([-2, 0, 0]) cube([2, 2, 2], center = true);
        translate([2, 0, 0]) sphere(r = 1.0);
    }
}

module overlap_intersection() {
    intersection() {
        cube([2, 2, 2], center = true);
        translate([0.4, 0, 0]) sphere(r = 1.35);
    }
}

module containment_difference() {
    difference() {
        cube([3, 3, 3], center = true);
        sphere(r = 0.75);
    }
}

module face_tangent_union() {
    union() {
        translate([-1, 0, 0]) cube([2, 2, 2], center = true);
        translate([1, 0, 0]) cube([2, 2, 2], center = true);
    }
}

module edge_tangent_union() {
    union() {
        translate([-1, -1, 0]) cube([2, 2, 2], center = true);
        translate([1, 1, 0]) cube([2, 2, 2], center = true);
    }
}

module vertex_tangent_union() {
    union() {
        translate([-1, -1, -1]) cube([2, 2, 2], center = true);
        translate([1, 1, 1]) cube([2, 2, 2], center = true);
    }
}

module offset_cylinder_union() {
    union() {
        sphere(r = 1.2);
        translate([0.35, -0.15, 0.0]) cylinder(h = 2.6, r = 0.55, center = true);
    }
}

module many_reductions_union() {
    union() {
        for (i = [-1:1]) {
            for (j = [-1:1]) {
                translate([i * 0.9, j * 0.9, 0]) sphere(r = 0.45);
            }
        }

        for (i = [-1:1]) {
            translate([i * 1.2, 0, 0]) cylinder(h = 2.0, r = 0.3, center = true);
            translate([0, i * 1.2, 0]) cylinder(h = 2.0, r = 0.3, center = true);
        }
    }
}

if (scenario == "disjoint_union") {
    disjoint_union();
} else if (scenario == "overlap_intersection") {
    overlap_intersection();
} else if (scenario == "containment_difference") {
    containment_difference();
} else if (scenario == "face_tangent_union") {
    face_tangent_union();
} else if (scenario == "edge_tangent_union") {
    edge_tangent_union();
} else if (scenario == "vertex_tangent_union") {
    vertex_tangent_union();
} else if (scenario == "offset_cylinder_union") {
    offset_cylinder_union();
} else if (scenario == "many_reductions_union") {
    many_reductions_union();
} else {
    assert(false, str("Unknown scenario: ", scenario));
}
