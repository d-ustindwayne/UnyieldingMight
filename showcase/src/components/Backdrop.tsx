import { Box, GlobalStyles } from '@mui/material';

// Global styles and the ambient glow behind the glass panels. Shared by every page.
export default function Backdrop() {
  return (
    <>
      <GlobalStyles
        styles={{
          html: { scrollBehavior: 'smooth' },
          body: { background: '#0E0E0E', overflowX: 'hidden' },
          '@keyframes fadeIn': { from: { opacity: 0, transform: 'translateY(4px)' }, to: { opacity: 1, transform: 'none' } },
          '@media (prefers-reduced-motion: reduce)': { html: { scrollBehavior: 'auto' }, '*': { animation: 'none !important' } },
        }}
      />
      <Box
        aria-hidden
        sx={{
          position: 'fixed', inset: 0, zIndex: -1, pointerEvents: 'none',
          background: `
            radial-gradient(600px 400px at 85% 10%, rgba(247,151,29,0.16), transparent 70%),
            radial-gradient(500px 500px at 5% 70%, rgba(143,211,168,0.07), transparent 70%),
            linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px) 0 0 / 100% 32px`,
        }}
      />
    </>
  );
}
