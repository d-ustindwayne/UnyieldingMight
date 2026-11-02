import { Box, Typography } from '@mui/material';
import { alpha } from '@mui/material/styles';
import { process } from '../data';
import { ember, glass, mono } from '../theme';

// Vertical timeline: a rail on the left, one glass row per phase.
export default function Process() {
  return (
    <Box component="ol" sx={{ m: 0, p: 0, listStyle: 'none', position: 'relative' }}>
      <Box aria-hidden sx={{ position: 'absolute', left: 7, top: 8, bottom: 8, width: '1px', bgcolor: 'rgba(255,255,255,0.12)' }} />
      {process.map((p) => (
        <Box component="li" key={p.phase} sx={{ position: 'relative', pl: 4.5, pb: 1.5 }}>
          <Box
            aria-hidden
            sx={{
              position: 'absolute', left: 2, top: 18, width: 11, height: 11, borderRadius: '50%',
              border: `2px solid ${p.pending ? 'rgba(255,255,255,0.3)' : ember}`,
              bgcolor: p.pending ? 'transparent' : alpha(ember, 0.3),
            }}
          />
          <Box
            sx={{
              ...glass, borderRadius: 2, px: { xs: 2, sm: 2.5 }, py: 1.75,
              display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '150px 1fr' }, gap: { xs: 1, sm: 2 },
              opacity: p.pending ? 0.7 : 1,
            }}
          >
            <Box>
              <Typography variant="h3">{p.phase}</Typography>
              <Typography sx={{ fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary' }}>{p.when}</Typography>
            </Box>
            <Box component="ul" sx={{ m: 0, pl: 2.25, '& li': { fontSize: '0.88rem', mb: 0.4 }, '& li::marker': { color: 'text.secondary' } }}>
              {p.items.map((i) => <li key={i}>{i}</li>)}
            </Box>
          </Box>
        </Box>
      ))}
    </Box>
  );
}
