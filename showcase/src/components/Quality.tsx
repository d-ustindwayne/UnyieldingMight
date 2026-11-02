import { Box, Paper, Typography } from '@mui/material';
import { courseworkMetrics, lessons } from '../data';
import { mono } from '../theme';
import Metrics from './Metrics';

const label = { fontFamily: mono, fontSize: '0.72rem', color: 'text.secondary' };

export default function Quality() {
  return (
    <>
      <Typography sx={{ ...label, mb: 1.25 }}>coursework baseline (Java)</Typography>
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr 1fr', sm: 'repeat(5, 1fr)' }, gap: 1.25, mb: 3 }}>
        {courseworkMetrics.map((m) => (
          <Paper key={m.label} sx={{ p: 1.75 }}>
            <Typography sx={{ fontFamily: mono, fontSize: '1.4rem', fontWeight: 700 }}>{m.value}</Typography>
            <Typography sx={{ fontSize: '0.8rem' }}>{m.label}</Typography>
            <Typography sx={{ ...label, fontSize: '0.66rem', mt: 0.5 }}>{m.note}</Typography>
          </Paper>
        ))}
      </Box>

      <Typography sx={{ ...label, mb: 1.25 }}>risks the coursework metrics raised, and the response</Typography>
      <Paper sx={{ p: 0, overflow: 'hidden', mb: 3 }}>
        {lessons.map((l, i) => (
          <Box
            key={l.from}
            sx={{
              display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 24px 1.3fr' }, alignItems: 'center',
              gap: { xs: 0.25, sm: 1.5 }, px: 2.5, py: 1.25, borderTop: i ? '1px solid rgba(255,255,255,0.05)' : 'none',
            }}
          >
            <Typography variant="body2" color="text.secondary">{l.from}</Typography>
            <Typography aria-hidden sx={{ fontFamily: mono, color: 'primary.main', display: { xs: 'none', sm: 'block' } }}>→</Typography>
            <Typography variant="body2">{l.to}</Typography>
          </Box>
        ))}
      </Paper>

      <Typography sx={{ ...label, mb: 1.25 }}>rewrite (Kotlin)</Typography>
      <Metrics />
    </>
  );
}
