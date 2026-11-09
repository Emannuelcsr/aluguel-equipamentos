export interface AtualizarClienteRequest {
  nomeRazaoSocial: string;
  nomeFantasia: string | null;
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
