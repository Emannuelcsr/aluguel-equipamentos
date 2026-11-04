import { Routes } from '@angular/router';
import { LoginComponent } from './features/autenticacao/login/login.component';
import { ListagemClientesComponent } from './features/clientes/listagem-clientes/listagem-clientes.component';
import { CadastroClienteComponent } from './features/clientes/cadastro-cliente/cadastro-cliente.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
    {
    path: 'clientes',
    component: ListagemClientesComponent
  },
  {
    path: 'clientes/novo',
    component: CadastroClienteComponent
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];
