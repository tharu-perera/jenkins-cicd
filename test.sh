branch123=develop
existed_in_local=$(git branch --list ${branch123})
if [[ -z ${existed_in_local} ]]; then
  echo '1'
  exit 1
else
  echo '0'
  exit 0
fi
