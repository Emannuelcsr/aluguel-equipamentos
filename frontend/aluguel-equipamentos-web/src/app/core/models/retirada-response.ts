
import { ItemRetiradaResponse } from "./item-retirada-response";
import { MetodoVerificacaoRetirada } from "./metodo-verificacao-retirada.enum";

export interface RetiradaResponse {

  id: number;
  reservaId : number;  
  dataHoraRetirada: string;  
  nomeRecebedor: string;
  documentoRecebedor:string;
  metodoVerificacao: MetodoVerificacaoRetirada;
  descricaoOutroMetodo: string | null;
  usuarioResponsavelId: number;  
  usuarioResponsavelNome:string
  itens:ItemRetiradaResponse[];
}
