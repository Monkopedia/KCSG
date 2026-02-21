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

module transformed_union_chain() {
    union() {
        translate([0.45, -0.3, 0.2])
            rotate([0, 0, 32])
            scale([1.2, 0.8, 1.1])
            cube([1.8, 1.2, 0.9], center = true);
        translate([-0.35, 0.45, -0.25])
            rotate([0, 90, 0])
            cylinder(h = 2.2, r = 0.35, center = true);
    }
}

module hull_tripod() {
    hull() {
        translate([-1.1, 0.0, 0.0]) cylinder(h = 1.2, r = 0.35, center = true);
        translate([1.1, 0.0, 0.0]) cylinder(h = 1.2, r = 0.35, center = true);
        translate([0.0, 1.2, 0.2]) cylinder(h = 1.2, r = 0.35, center = true);
    }
}

module extrude_profile_difference() {
    difference() {
        linear_extrude(height = 1.6)
            polygon(points = [
                [-1.2, -0.4],
                [1.0, -0.7],
                [1.3, 0.3],
                [0.1, 1.0],
                [-1.0, 0.7]
            ]);
        translate([0.2, 0.1, -0.1]) cylinder(h = 1.8, r = 0.35, center = false);
    }
}

module mirrored_intersection() {
    intersection() {
        union() {
            translate([0.6, 0, 0]) cube([1.2, 1.6, 1.0], center = true);
            mirror([0, 1, 0]) translate([0.6, 0.35, 0]) cube([1.2, 1.6, 1.0], center = true);
        }
        translate([0.15, 0.0, 0.0]) sphere(r = 1.1);
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
} else if (scenario == "transformed_union_chain") {
    transformed_union_chain();
} else if (scenario == "hull_tripod") {
    hull_tripod();
} else if (scenario == "extrude_profile_difference") {
    extrude_profile_difference();
} else if (scenario == "mirrored_intersection") {
    mirrored_intersection();
} else {
    assert(false, str("Unknown scenario: ", scenario));
}
