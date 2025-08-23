import { paths } from 'src/routes/paths';

import { CONFIG } from 'src/global-config';

import { Label } from 'src/components/label';
import { SvgColor } from 'src/components/svg-color';

// ----------------------------------------------------------------------

const icon = (name) => <SvgColor src={`${CONFIG.assetsDir}/assets/icons/navbar/${name}.svg`} />;

const ICONS = {
  job: icon('ic-job'),
  blog: icon('ic-blog'),
  chat: icon('ic-chat'),
  mail: icon('ic-mail'),
  user: icon('ic-user'),
  file: icon('ic-file'),
  lock: icon('ic-lock'),
  tour: icon('ic-tour'),
  order: icon('ic-order'),
  label: icon('ic-label'),
  blank: icon('ic-blank'),
  kanban: icon('ic-kanban'),
  folder: icon('ic-folder'),
  course: icon('ic-course'),
  params: icon('ic-params'),
  banking: icon('ic-banking'),
  booking: icon('ic-booking'),
  invoice: icon('ic-invoice'),
  product: icon('ic-product'),
  calendar: icon('ic-calendar'),
  disabled: icon('ic-disabled'),
  external: icon('ic-external'),
  subpaths: icon('ic-subpaths'),
  menuItem: icon('ic-menu-item'),
  ecommerce: icon('ic-ecommerce'),
  analytics: icon('ic-analytics'),
  dashboard: icon('ic-dashboard'),
};

// ----------------------------------------------------------------------

export const navData = [
  /**
   * MediHelp360 Dashboard
   */
  {
    subheader: 'Dashboard',
    items: [
      {
        title: 'Dashboard',
        path: paths.dashboard.root,
        icon: ICONS.dashboard,
        info: <Label>v{CONFIG.appVersion}</Label>,
      },
      {
        title: 'Estado del Sistema',
        path: paths.dashboard.systemStatus,
        icon: ICONS.analytics,
      },
    ],
  },
  /**
   * Gestión de Usuarios
   */
  {
    subheader: 'Gestión de Usuarios',
    items: [
      {
        title: 'Usuarios',
        path: paths.dashboard.users,
        icon: ICONS.user,
      },
      {
        title: 'Roles',
        path: paths.dashboard.roles,
        icon: ICONS.lock,
      },
    ],
  },
  /**
   * Servicios de Sincronización
   */
  {
    subheader: 'Servicios de Datos',
    items: [
      {
        title: 'Servicios de Sync',
        path: paths.dashboard.syncServices.root,
        icon: ICONS.folder,
        children: [
          {
            title: 'Sync Service A (PostgreSQL)',
            path: paths.dashboard.syncServices.serviceA,
          },
          {
            title: 'Sync Service B (MySQL)',
            path: paths.dashboard.syncServices.serviceB,
          },
          {
            title: 'Sync Service C (MongoDB)',
            path: paths.dashboard.syncServices.serviceC,
          },
        ],
      },
    ],
  },
];
