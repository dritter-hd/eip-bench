package com.eipbench.content;

public class Constants {
    public enum TYPE {
        CAMEL, DATALOG, CAMEL_CONVERSION, DATALOG_CONVERSION, CAMEL_BASELINE, CAMEL_LB_BASELINE, DATALOG_BASELINE,
        TPC_H_ORDERS_1_5_MIO_NOOP,
        // Datalog
        TPC_H_EIP_CD_CBR_A,
        TPC_H_EIP_CD_CBR_B,
        TPC_H_EIP_CD_CBR_C,
        TPC_H_EIP_CD_CBR_D,
        TPC_H_EIP_CD_EO_A, TPC_H_EIP_CD_EO_SD_A,
        TPC_H_EIP_CD_EO_B, TPC_H_EIP_CD_EO_SD_B,
        TPC_H_EIP_CD_EOIO_A,
        TPC_H_EIP_CD_ID_A, TPC_H_EIP_CD_ID_SD_A,
        TPC_H_EIP_CD_ID_E, TPC_H_EIP_CD_ID_SD_E,
        TPC_H_EIP_CD_MC_A, TPC_H_EIP_CD_MC_NP_A,
        TPC_H_EIP_CD_MC_B, TPC_H_EIP_CD_MC_NP_B,
        TPC_H_EIP_CD_MC_C, TPC_H_EIP_CD_MC_NP_C,
        TPC_H_EIP_CD_MCMF_A, TPC_H_EIP_CD_MCMF_NP_A,
        TPC_H_EIP_CD_MCMF_B, TPC_H_EIP_CD_MCMF_NP_B,
        TPC_H_EIP_CD_MCMF_C, TPC_H_EIP_CD_MCMF_NP_C,
        TPC_H_EIP_CD_MF_A, TPC_H_EIP_CD_MF_NT_A,
        TPC_H_EIP_CD_MF_B,
        TPC_H_EIP_CD_MF_C,
        TPC_H_EIP_CD_MT_A,
        TPC_H_EIP_CD_RL_A,
        TPC_H_EIP_CD_RS_A,
        TPC_H_EIP_CD_SP_B,
        TPC_H_EIP_CD_SP_C,
        TPC_H_EIP_CD_CBR_SCALE_A,
        TPC_H_EIP_CD_CBR_SCALE_B,
        TPC_H_EIP_CD_CBR_SCALE_C,
        
        // Java
        TPC_H_EIP_CJ_ALO_A, TPC_H_EIP_CJ_ALO_OM_A,
        TPC_H_EIP_CJ_ALO_B, TPC_H_EIP_CJ_ALO_OM_B,
        TPC_H_EIP_CJ_ALO_C, TPC_H_EIP_CJ_ALO_OM_C,
        TPC_H_EIP_CJ_ALO_D,
        TPC_H_EIP_CJ_ALO_E,
        TPC_H_EIP_CJ_ALO_F,
        TPC_H_EIP_CJ_AG_A,
        TPC_H_EIP_CJ_AGSP_A,
        TPC_H_EIP_CJ_AGSP_B,
        TPC_H_EIP_CJ_CBR_A, TPC_H_EIP_CJ_CBR_NO_A,
        TPC_H_EIP_CJ_CBR_B, TPC_H_EIP_CJ_CBR_NO_B,
        TPC_H_EIP_CJ_CBR_C, TPC_H_EIP_CJ_CBR_NO_C,
        TPC_H_EIP_CJ_CBR_D, TPC_H_EIP_CJ_CBR_FM_D,
        TPC_H_EIP_CJ_CBR_E,
        TPC_H_EIP_CJ_EO_A, TPC_H_EIP_CJ_EO_SD_A, TPC_H_EIP_CJ_EO_HD_A, TPC_H_EIP_CJ_EO_HD_SD_A,
        TPC_H_EIP_CJ_EO_B, TPC_H_EIP_CJ_EO_SD_B, TPC_H_EIP_CJ_EO_HD_B, TPC_H_EIP_CJ_EO_HD_SD_B,
        TPC_H_EIP_CJ_EOIO_A, TPC_H_EIP_CJ_EOIO_HDI_A,
        TPC_H_EIP_CJ_EOIO_HDR_A, TPC_H_EIP_CJ_EOIO_HDI_HDR_A,
        TPC_H_EIP_CJ_ID_A, TPC_H_EIP_CJ_ID_SD_A, TPC_H_EIP_CJ_ID_HD_A, TPC_H_EIP_CJ_ID_HD_SD_A,
        TPC_H_EIP_CJ_ID_HD_B,
        TPC_H_EIP_CJ_ID_HD_C,
        TPC_H_EIP_CJ_ID_HD_D,
        TPC_H_EIP_CJ_ID_E, TPC_H_EIP_CJ_ID_SD_E, TPC_H_EIP_CJ_ID_HD_E, TPC_H_EIP_CJ_ID_HD_SD_E,
        TPC_H_EIP_CJ_ID_HD_F,
        TPC_H_EIP_CJ_MC_A, TPC_H_EIP_CJ_MC_NP_A,
        TPC_H_EIP_CJ_MC_B, TPC_H_EIP_CJ_MC_NP_B,
        TPC_H_EIP_CJ_MC_C, TPC_H_EIP_CJ_MC_NP_C,
        TPC_H_EIP_CJ_MCMF_A, TPC_H_EIP_CJ_MCMF_NP_A,
        TPC_H_EIP_CJ_MCMF_B, TPC_H_EIP_CJ_MCMF_NP_B,
        TPC_H_EIP_CJ_MCMF_C, TPC_H_EIP_CJ_MCMF_NP_C,
        TPC_H_EIP_CJ_MF_A, TPC_H_EIP_CJ_MF_NT_A,
        TPC_H_EIP_CJ_MF_B,
        TPC_H_EIP_CJ_MF_C,
        TPC_H_EIP_CJ_MT_A,
        TPC_H_EIP_CJ_MT_B,
        TPC_H_EIP_CJ_MT_C,
        TPC_H_EIP_CJ_MT_D,
        TPC_H_EIP_CJ_MT_E,
        TPC_H_EIP_CJ_MT_F,
        TPC_H_EIP_CJ_MT_G,
        TPC_H_EIP_CJ_MT_H,
        TPC_H_EIP_CJ_MT_I,
        TPC_H_EIP_CJ_MT_J,
        TPC_H_EIP_CJ_MT_K,
        TPC_H_EIP_CJ_MT_L,
        TPC_H_EIP_CJ_MT_M,
        TPC_H_EIP_CJ_RL_A,
        TPC_H_EIP_CJ_RS_HD_A,
        TPC_H_EIP_CJ_RS_A,
        TPC_H_EIP_CJ_SP_A,
        TPC_H_EIP_CJ_SP_B,
        TPC_H_EIP_CJ_SP_C,
        // Java - Load Balancer
        TPC_H_EIP_CJ_LBAG_A,
        TPC_H_EIP_CJ_LBCBR_A,
        TPC_H_EIP_CJ_LBMC_A,
        TPC_H_EIP_CJ_LBMC_B,
        TPC_H_EIP_CJ_LBMC_C,
        TPC_H_EIP_CJ_LBMF_A,
        TPC_H_EIP_CJ_LBMT_A,
        TPC_H_EIP_CJ_LBMT_M,
        TPC_H_EIP_CJ_LBRL_A,
        TPC_H_EIP_CJ_LBSP_A, 
        // Java - scale data size
        TPC_H_EIP_CJ_CBR_SCALE_A, 
        TPC_H_EIP_CJ_CBR_SCALE_NO_A, 
        TPC_H_EIP_CJ_CBR_SCALE_B, 
        TPC_H_EIP_CJ_CBR_SCALE_NO_B, 
        TPC_H_EIP_CJ_CBR_SCALE_C, 
        TPC_H_EIP_CJ_CBR_SCALE_NO_C,
        
        // Image
        FACE_EIP_IMAGE_BL,
        FACE_EIP_IMAGE_CBR_A, FACE_EIP_IMAGE_CBR_A_RDF_ONLY, FACE_EIP_IMAGE_CBR_A_IMAGE_ONLY, 
        FACE_EIP_IMAGE_SP_A, FACE_EIP_IMAGE_SP_B_Fixed,
        FACE_EIP_IMAGE_MT_A, FACE_EIP_IMAGE_MT_B, FACE_EIP_IMAGE_MT_A_MarkFixed,
        FACE_EIP_IMAGE_SCENARIO,
        
        // Ocr
        INVOICE_EIP_OCR_BL,
        INVOICE_EIP_OCR_CBR_A,
        INVOICE_EIP_OCR_CBR_A_LB,
        
        // FPGA
        TPC_H_EIP_HW_CBR_A;

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
