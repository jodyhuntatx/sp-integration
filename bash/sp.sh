#!/bin/bash

# Does not require authentication:
#  getConfig | jq .
#
# Requires authentication:
#  getSchemas $ACCESS_TOKEN | jq .
#  getResourceTypes $ACCESS_TOKEN | jq .
#
# User:
#  List all userNames. userName field does not have any whitespace and is used for queries.
#    getAllUsers $ACCESS_TOKEN | jq .Resources[].userName
#  Create new user. userName will by $FName.$LName
#    createUser $ACCESS_TOKEN $FName $LName
#  getUserByName $ACCESS_TOKEN $FName.$LName
#  Get user ID:
#    userId=$(getUserByName $ACCESS_TOKEN $FName.$LName | jq -r .id)
#  getUserById $ACCESS_TOKEN $userId | jq .
#  deleteUserById $ACCESS_TOKEN $userId
#  getUserRolesEntitlementsById $ACCESS_TOKEN $userId | jq .
#
# AD Accounts:
#  getAllADAccounts $ACCESS_TOKEN
#  getADAccountsByUserId $ACCESS_TOKEN $userId
#  getADAccountByAccountId $ACCESS_TOKEN $AccountId
#
# Entitlements: 
#  getAllEntitlements $ACCESS_TOKEN
#  getEntitlementsByDisplayName $ACCESS_TOKEN $entitlementDisplayName
#  getEntitlementById $ACCESS_TOKEN $eId
#
# Roles:
#  getAllRoles $ACCESS_TOKEN
#  getRole $ACCESS_TOKEN $roleId
#
# Policy Violations:
#  getAllPolicyViolations $ACCESS_TOKEN
#  getPolicyViolation $ACCESS_TOKEN $pvId
#
# Workflows:
#  getAllWorkflows $ACCESS_TOKEN
#XX  launchWorkflow $ACCESS_TOKEN | jq .
#  Get names of launched workflows:
#    getAllLaunchedWorkflows $ACCESS_TOKEN | jq -jr '.Resources[] | "name: ",.name,"\n  id: ",.id,"\n"'
#  getWorkflowStatus $ACCESS_TOKEN $workflowId
#
# TaskResults:
#  getAllTaskResults $ACCESS_TOKEN
#  getTaskResult $ACCESS_TOKEN $taskId
#
# Applications:
#  List app names and IDs:
#    getAllApplications $ACCESS_TOKEN | jq -jr '.Resources[] | "name: ",.name,"\n  id: ",.id,"\n"'
#  getApplicationById $ACCESS_TOKEN $appId
#

source ./users.bashlib
source ./applications.bashlib
source ./accounts.bashlib
source ./entitlements.bashlib
source ./roles.bashlib
source ./policyviolations.bashlib
source ./workflows.bashlib
source ./taskresults.bashlib

export CURL="curl -s"
export BASE_URL=http://localhost:8080/iiq
export SCIM_URL=$BASE_URL/scim/v2

# Authentication values from SailPoint UI:
export CLIENT_ID=zYoP6mTxgF7IP3vnnFo29XfTEZ6wl6bf
export SECRET=6SPSyAScjGF8Ssvf

# Values for testing
FName=Bill
LName=Moore
userId=8a8080824df45873014df45c299c0231
entitlementDisplayName=TRAKK
appId=8a8080824df44d48014df45444c002da
workflowId=8a8080815656880101565688a64d0038
taskId=
roleId=
pvId=
eId=


main() {
  ACCESS_TOKEN=$(getAuthToken $CLIENT_ID $SECRET | jq -r .access_token)
  getAllUsers $ACCESS_TOKEN | jq .Resources[].userName
}

main "$@"
