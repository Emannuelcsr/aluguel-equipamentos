export interface UsuarioAutenticadoResponse {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMINISTRADOR' | 'FUNCIONARIO';
}