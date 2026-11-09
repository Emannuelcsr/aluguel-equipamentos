import { Routes } from '@angular/router';
import { LoginComponent } from './features/autenticacao/login/login.component';
import { ListagemClientesComponent } from './features/clientes/listagem-clientes/listagem-clientes.component';
import { CadastroClienteComponent } from './features/clientes/cadastro-cliente/cadastro-cliente.component';
import { autenticacaoGuard } from './core/guards/autenticacao.guard';
import { loginGuard } from './core/guards/login.guard';
import { LayoutComponent } from './layout/layout/layout.component';
import { DetalhesClienteComponent } from './features/clientes/detalhes-cliente/detalhes-cliente.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [loginGuard],
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [autenticacaoGuard],
    children: [
      {
        path: 'clientes',
        component: ListagemClientesComponent,
      },
      {
        path: 'clientes/novo',
        component: CadastroClienteComponent,
      },
      {
        path: 'clientes/detalhes/:id',
        component: DetalhesClienteComponent,
      },
    ],
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'clientes',
  },
];
