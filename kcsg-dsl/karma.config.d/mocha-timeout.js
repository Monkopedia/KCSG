// Karma runs the browser suites through mocha, whose per-test timeout defaults
// to 2s. The geometry suites blow past that for the same reason the nodejs test
// tasks raise it in build.gradle.kts: the S8 invariance scenario walks a full
// pair x opt-type boolean matrix. Keep this in step with the `useMocha` timeouts
// there.
//
// This is set from karma.config.d rather than `browser { testTask { useMocha { .. } } }`
// on purpose: that DSL call *replaces* karma with a bare node mocha runner, which
// both changes the npm dependency set (invalidating kotlin-js-store/yarn.lock) and
// stops the tests running in a browser at all.
//
// Mutate the existing client config rather than config.set({client: ...}) — a
// wholesale set would drop `client.args`, which the kotlin karma runner needs.
config.client = config.client || {};
config.client.mocha = config.client.mocha || {};
config.client.mocha.timeout = 120000;
