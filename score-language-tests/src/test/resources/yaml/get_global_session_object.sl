#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

imports:
  props: test.sys.props

operation:
  name: get_global_session_object
  inputs:
    - value
  action:
    java_action:
      className: org.openscore.lang.systemtests.actions.LangTestActions
      methodName: getConnectionFromNonSerializableSession
  outputs:
    - session_object_value: connection
  results:
    - SUCCESS: connection == fromInputs['value']
    - FAILURE
