#!/bin/bash
br="$1"
#function is_in_local() {
#    local branch=$br
#    echo $branch
#    local existed_in_local=$(git branch --list ${branch})
#
#    if [[ -z ${existed_in_local} ]]; then
#        echo 0
#    else
#        echo 1
#    fi
#}

function is_in_remote() {
    local branch=$br
    local existed_in_remote=$(git ls-remote --heads origin ${branch})

    if [[ -z ${existed_in_remote} ]]; then
        echo 0
    else
        echo 1
    fi
}

#is_in_local br
is_in_remote br
