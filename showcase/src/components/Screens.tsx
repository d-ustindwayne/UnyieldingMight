import { Box, Typography } from '@mui/material';
import { screens } from '../data';
import { mono } from '../theme';

// Horizontal filmstrip; scrolls inside its own box so the page never scrolls sideways.
export default function Screens() {
  return (
    <Box
      sx={{
        display: 'flex', gap: 2, overflowX: 'auto', pb: 1.5, scrollSnapType: 'x mandatory',
        '&::-webkit-scrollbar': { height: 6 },
        '&::-webkit-scrollbar-thumb': { background: 'rgba(255,255,255,0.15)', borderRadius: 3 },
      }}
    >
      {screens.map((s) => (
        <Box key={s.src} component="figure" sx={{ m: 0, flex: 'none', scrollSnapAlign: 'start' }}>
          <Box
            component="img"
            src={s.src}
            alt={s.label}
            loading="lazy"
            sx={{
              display: 'block', height: { xs: 300, sm: 380 }, width: 'auto', borderRadius: 2.5,
              border: '1px solid rgba(255,255,255,0.08)', boxShadow: '0 20px 40px -20px rgba(0,0,0,0.9)',
            }}
          />
          <Typography component="figcaption" sx={{ fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary', mt: 1 }}>
            {s.label}
          </Typography>
        </Box>
      ))}
    </Box>
  );
}
