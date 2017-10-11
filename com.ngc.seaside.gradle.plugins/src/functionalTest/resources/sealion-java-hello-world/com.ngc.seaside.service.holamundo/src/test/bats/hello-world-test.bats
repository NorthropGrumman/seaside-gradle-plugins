#!/usr/bin/env bats -t

load helpers

@test "When I run the script, it should print '$message' to the screen" {
    run $script "$message"

    [ $status -eq 0 ]
    [ "$output" = "$message" ]
}
