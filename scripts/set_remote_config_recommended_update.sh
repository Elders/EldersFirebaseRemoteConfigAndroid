#!/bin/bash

#the script can be executed with variables, for example:
# ./set_remote_config_recommended_update.sh (android|ios) (version) (download location) (service account jason location)
platform=$1
recommended_update_verison=$2
recommended_update_download=$3
service_account_json=$4

service_endpoint="https://firebaseremoteconfig.googleapis.com"

# check that all variables are present
if [ -z "$platform" ] || [ -z "$recommended_update_verison" ] || [ -z "$recommended_update_download" ] || [ -z "$service_account_json" ]; then
    echo "Usage: ./set_remote_config_recommended_update.sh (android|ios) (version) (download location) (service account jason location)"
    exit 1
fi

service_account_json=$(cat $service_account_json)

project_id=$(echo -n $service_account_json | jq -r .project_id)
private_key_id=$(echo -n $service_account_json | jq -r .private_key_id)
private_key=$(echo -n $service_account_json | jq .private_key)
client_email=$(echo -n $service_account_json | jq -r .client_email)
token_uri=$(echo -n $service_account_json | jq -r .token_uri)

# required scopes for firebase remote config APIs
scope="https://www.googleapis.com/auth/firebase.remoteconfig https://www.googleapis.com/auth/cloud-platform"

# fixing private key and saving to file
private_key=$(echo "${private_key//\\n/$'\n'}")
echo "${private_key//\"/$''}" > private.key

# composing JWT parts
time_now=$(date +%s)
expiration=$(($time_now + 3600))

header=$( jq -n --arg kid $private_key_id --arg typ "JWT" --arg alg "RS256" '{alg: $alg, kid: $kid, typ: $typ}' )
payload=$( jq -n --arg client_email $client_email --arg token_uri $token_uri --arg scope "$scope" --argjson iat $time_now --argjson exp $expiration \
       '{iss: $client_email, sub: $client_email, aud: $token_uri, scope: $scope, iat: $iat, exp: $exp}' )

header_base64=$(echo -n $header | openssl base64 -e -A | sed s/\\+/-/g | sed s/\\//_/g | sed -E s/=+$//)
payload_base64=$(echo -n $payload | openssl base64 -e -A | sed s/\\+/-/g | sed s/\\//_/g | sed -E s/=+$//)

data_to_sign=$header_base64'.'$payload_base64

# generating token signature 
echo -n $data_to_sign | openssl dgst -sha256 -sign private.key -out signature.bin

# encode signature base64 
signature_base64=$(openssl base64 -in signature.bin -e -A | sed s/\\+/-/g | sed s/\\//_/g | sed -E s/=+$//)

generated_token=$data_to_sign'.'$signature_base64

# compose post data for token request
post_json=$( jq -n --arg grant_type "urn:ietf:params:oauth:grant-type:jwt-bearer" --arg assertion $generated_token '{grant_type: $grant_type, assertion: $assertion}' )

#request token
token_response=$(curl -s \
    -H "Accept: application/json" \
    -H "Content-Type:application/json" \
    -X POST -d "$post_json" $token_uri)

# get the token from response and check it is not empty
access_token=$(echo -n $token_response | jq .access_token)

if [ -z "$access_token" ]; then
    echo "ERROR: Access token is empty"
    exit 1
fi

# get current remote configuration
remote_config_endpoint=$service_endpoint'/v1/projects/'$project_id'/remoteConfig'

remote_config=$(curl -s \
    -H "Authorization: Bearer $access_token" \
    -H "Accept: */*" \
    -H "Content-Type:application/json" \
    -X GET $remote_config_endpoint)

# check that the configuration is not empty
if [ -z "$remote_config" ]; then
    echo "ERROR: Remote config is empty"
    exit 1
fi

# check platform and moddify json with parameters from input
if [ "$platform" = "android" ]; then
    remote_config=$(echo $remote_config | jq '.parameters.android_recommended_update.defaultValue.value = "{\"version\": \"'"$recommended_update_verison"'\", \"download\": \"'"$recommended_update_download"'\"}"')
elif [ "$platform" = "ios" ]; then
    remote_config=$(echo $remote_config | jq '.parameters.ios_recommended_update.defaultValue.value = "{\"version\": \"'"$recommended_update_verison"'\", \"download\": \"'"$recommended_update_download"'\"}"')
else
    echo "ERROR: Platform parameter can be android or ios"
    exit 1
fi


# put the updated remote configuration to firebase with If-Match: *, !!!!! this is force update !!!!!
# current default version of curl is not aware of ETag.
# From version 7.68.0 ETag can be saved to file with --etag-save <filename>, but alo does not work
result=$(curl -s \
    -H "Authorization: Bearer $access_token" \
    -H "If-Match: *" \
    -H "Accept: */*" \
    -H "Content-Type:application/json" \
    -X PUT -d "$remote_config" $remote_config_endpoint)

echo $result
