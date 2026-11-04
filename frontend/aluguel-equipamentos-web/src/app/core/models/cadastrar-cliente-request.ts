export interface CadastrarClienteRequest {
  tipo: 'PF' | 'PJ';
  nomeRazaoSocial: string;
  nomeFantasia: string | null;
  documento: string;
  email: string;
  telefone: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  cidade: string;
  estado: string;
}
