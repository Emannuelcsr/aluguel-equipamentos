import { UsuarioAutenticadoResponse } from "./usuario-autenticado-response";

export interface LoginResponse {
  token: string;
  tipo: string;
  expiraEm: string;
  usuario: UsuarioAutenticadoResponse;
}