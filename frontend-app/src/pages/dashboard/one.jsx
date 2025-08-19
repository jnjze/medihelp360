import { CONFIG } from 'src/global-config';

import { DashboardView } from 'src/sections/dashboard/view';

// ----------------------------------------------------------------------

const metadata = { title: `Dashboard | MediHelp360 - ${CONFIG.appName}` };

export default function Page() {
  return (
    <>
      <title>{metadata.title}</title>

      <DashboardView />
    </>
  );
}
