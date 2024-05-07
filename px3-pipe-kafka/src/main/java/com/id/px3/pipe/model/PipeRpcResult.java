package com.id.px3.pipe.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class PipeRpcResult extends LinkedHashMap<String, Object> {

    public static final String RPC_OK = "___rpc_ok";
    public static final String RPC_ERROR = "___rpc_error";

    public static PipeRpcResult ok(Map<String, Object> result) {
        PipeRpcResult rpcResult = new PipeRpcResult();
        rpcResult.put(RPC_OK, true);
        rpcResult.putAll(result);
        return rpcResult;
    }

    public static PipeRpcResult error(String error) {
        PipeRpcResult rpcResult = new PipeRpcResult();
        rpcResult.put(RPC_OK, false);
        rpcResult.put(RPC_ERROR, error);
        return rpcResult;
    }

    public PipeRpcResult() {
    }

    public PipeRpcResult(Map<String, Object> src) {
        super(src);
    }
}
