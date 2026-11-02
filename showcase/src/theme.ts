import { createTheme, alpha } from '@mui/material/styles';

// Palette taken from the Android app (ui/theme/Theme.kt): UmBlack + Ember.
export const ember = '#F7971D';
export const mono = '"JetBrains Mono", ui-monospace, monospace';

export const glass = {
  background: 'rgba(255,255,255,0.035)',
  backdropFilter: 'blur(18px) saturate(140%)',
  WebkitBackdropFilter: 'blur(18px) saturate(140%)',
  border: '1px solid rgba(255,255,255,0.08)',
  boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.06), 0 20px 40px -24px rgba(0,0,0,0.8)',
};

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: ember, contrastText: '#1F1200' },
    secondary: { main: '#8FD3A8' },
    background: { default: '#0E0E0E', paper: 'rgba(255,255,255,0.035)' },
    text: { primary: '#F2F2F2', secondary: '#9A9A9A' },
    divider: 'rgba(255,255,255,0.08)',
  },
  shape: { borderRadius: 14 },
  typography: {
    fontFamily: '"IBM Plex Sans", system-ui, sans-serif',
    h1: { fontFamily: mono, fontWeight: 700, fontSize: 'clamp(2rem, 6vw, 3.4rem)', letterSpacing: '-0.03em' },
    h2: { fontFamily: mono, fontWeight: 500, fontSize: '0.8rem', letterSpacing: '0.14em', textTransform: 'uppercase' },
    h3: { fontWeight: 600, fontSize: '1.05rem' },
    body1: { fontSize: '0.95rem', lineHeight: 1.6 },
    body2: { fontSize: '0.85rem', lineHeight: 1.55 },
  },
  components: {
    MuiPaper: { styleOverrides: { root: { ...glass, backgroundImage: 'none' } } },
    MuiChip: {
      styleOverrides: {
        root: { fontFamily: mono, fontSize: '0.72rem', borderColor: 'rgba(255,255,255,0.12)' },
      },
    },
    MuiButton: { styleOverrides: { root: { textTransform: 'none', fontFamily: mono, fontWeight: 500 } } },
    MuiTab: {
      styleOverrides: {
        root: { textTransform: 'none', fontFamily: mono, fontSize: '0.8rem', minHeight: 40, alignItems: 'flex-start' },
      },
    },
    MuiAccordion: {
      styleOverrides: {
        root: {
          ...glass,
          '&:before': { display: 'none' },
          '&.Mui-expanded': { margin: 0, borderColor: alpha(ember, 0.35) },
        },
      },
    },
    MuiTooltip: { styleOverrides: { tooltip: { fontFamily: mono, fontSize: '0.7rem' } } },
  },
});
