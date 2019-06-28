package com.eipbench.content;

public class Constants {
    public enum TYPE {
        CAMEL, DATALOG, CAMEL_CONVERSION, DATALOG_CONVERSION, CAMEL_BASELINE, DATALOG_BASELINE,
        // Datalog
        TPC_H_EIP_CD_CBR_A,
        TPC_H_EIP_CD_CBR_B,
        TPC_H_EIP_CD_CBR_C,
        TPC_H_EIP_CD_CBR_D,

        TPC_H_EIP_CD_CBR_SCALE_A,
        TPC_H_EIP_CD_CBR_SCALE_B,
        TPC_H_EIP_CD_CBR_SCALE_C,
        
        // Java
        TPC_H_EIP_CJ_CBR_A, TPC_H_EIP_CJ_CBR_NO_A,
        TPC_H_EIP_CJ_CBR_B, TPC_H_EIP_CJ_CBR_NO_B,
        TPC_H_EIP_CJ_CBR_C, TPC_H_EIP_CJ_CBR_NO_C,
        TPC_H_EIP_CJ_CBR_D, TPC_H_EIP_CJ_CBR_FM_D,
        TPC_H_EIP_CJ_CBR_E,

        // Java - scale data size
        TPC_H_EIP_CJ_CBR_SCALE_A, 
        TPC_H_EIP_CJ_CBR_SCALE_NO_A, 
        TPC_H_EIP_CJ_CBR_SCALE_B, 
        TPC_H_EIP_CJ_CBR_SCALE_NO_B, 
        TPC_H_EIP_CJ_CBR_SCALE_C, 
        TPC_H_EIP_CJ_CBR_SCALE_NO_C,

        TPC_H_EIP_BEAM_CBR_A;

        public String buildId(String subId, final String delimiter) {
            return this.name() + delimiter + subId;
        }

        public String buildUriPart(final FORMAT format, final String delimiter) {
            return this.name() + delimiter + format.name();
        }
    }

    public enum FORMAT {
        JSON, CSV
    }
}
