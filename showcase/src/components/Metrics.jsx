import { Box, Paper, Stack, Typography } from '@mui/material';
import { metrics } from '../data';
import { ember, mono } from '../theme';

const total = metrics.modules.reduce((sum, m) => sum + m.lines, 0);

export default function Metrics() {
  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1.6fr 1fr' }, gap: 2 }}>
      <Paper sx={{ p: { xs: 2.5, sm: 3 } }}>
        <Typography sx={{ fontFamily: mono, fontSize: '0.72rem', color: 'text.secondary', mb: 2 }}>
          Kotlin source · {total.toLocaleString('en')} lines
        </Typography>
        <Stack spacing={2}>
          {metrics.modules.map((m) => (
            <Box key={m.name}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', fontFamily: mono, fontSize: '0.8rem', mb: 0.75 }}>
                <span>{m.name}/</span>
                <Box component="span" sx={{ color: 'text.secondary' }}>
                  {m.lines.toLocaleString('en')} · {m.files} files
                </Box>
              </Box>
              <Box sx={{ height: 6, borderRadius: 3, bgcolor: 'rgba(255,255,255,0.06)', overflow: 'hidden' }}>
                <Box
                  role="presentation"
                  sx={{ height: '100%', width: `${(m.lines / total) * 100}%`, borderRadius: 3, background: `linear-gradient(90deg, ${ember}, #FFC473)` }}
                />
              </Box>
            </Box>
          ))}
        </Stack>
      </Paper>

      <Paper sx={{ p: { xs: 2.5, sm: 3 }, display: 'flex', flexDirection: 'column' }}>
        <Typography sx={{ fontFamily: mono, fontSize: '0.72rem', color: 'text.secondary' }}>automated tests</Typography>
        <Typography sx={{ fontFamily: mono, fontSize: '2.75rem', fontWeight: 700, lineHeight: 1.1, my: 1 }}>{metrics.tests}</Typography>
        <Box component="ul" sx={{ m: 0, pl: 2, mb: 2, color: 'text.secondary', '& li': { fontSize: '0.82rem', mb: 0.5 } }}>
          {metrics.testKinds.map((t) => <li key={t}>{t}</li>)}
        </Box>
        <Box sx={{ mt: 'auto', fontFamily: mono, fontSize: '0.72rem', color: 'secondary.main' }}>● GitHub Actions on every push</Box>
      </Paper>
    </Box>
  );
}
