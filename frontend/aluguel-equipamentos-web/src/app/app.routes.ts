import { Routes } from '@angular/router';
import { LoginComponent } from './features/autenticacao/login/login.component';
import { ListagemClientesComponent } from './features/clientes/listagem-clientes/listagem-clientes.component';
import { CadastroClienteComponent } from './features/clientes/cadastro-cliente/cadastro-cliente.component';
import { autenticacaoGuard } from './core/guards/autenticacao.guard';
import { loginGuard } from './core/guards/login.guard';
import { LayoutComponent } from './layout/layout/layout.component';
import { DetalhesClienteComponent } from './features/clientes/detalhes-cliente/detalhes-cliente.component';
import { EditarClientesComponent } from './features/clientes/editar-clientes/editar-clientes.component';
import { ListagemCategoriasComponent } from './features/categoria/listagem-categorias/listagem-categorias.component';
import { CadastroCategoriaComponent} from './features/categoria/cadastro-categoria/categoria.component';

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
      {
        path: 'clientes/editar/:id',
        component: EditarClientesComponent,
      },
            {
        path: 'categorias',
        component: ListagemCategoriasComponent,
      },
            {
        path: 'categorias/novo',
        component: CadastroCategoriaComponent,
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
