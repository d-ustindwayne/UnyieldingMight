import { Box, Paper, Typography } from '@mui/material';
import { patterns } from '../data';
import { mono } from '../theme';

// The four patterns from the coursework, and what each became in Kotlin.
export default function Patterns() {
  return (
    <Paper sx={{ p: 0, overflow: 'hidden' }}>
      {patterns.map((p, i) => (
        <Box
          key={p.name}
          sx={{
            display: 'grid', gridTemplateColumns: { xs: '1fr', md: '150px 1fr 1fr' }, gap: { xs: 1, md: 2.5 },
            px: { xs: 2, sm: 2.5 }, py: 2, borderTop: i ? '1px solid rgba(255,255,255,0.06)' : 'none',
          }}
        >
          <Box>
            <Typography variant="h3">{p.name}</Typography>
            <Typography sx={{ fontFamily: mono, fontSize: '0.7rem', color: 'text.secondary' }}>{p.kind}</Typography>
          </Box>
          <Box>
            <Typography sx={{ fontFamily: mono, fontSize: '0.68rem', color: 'text.secondary', mb: 0.25 }}>java · coursework</Typography>
            <Typography variant="body2" color="text.secondary">{p.then}</Typography>
          </Box>
          <Box>
            <Typography sx={{ fontFamily: mono, fontSize: '0.68rem', color: 'primary.main', mb: 0.25 }}>kotlin · rewrite</Typography>
            <Typography variant="body2">{p.now}</Typography>
          </Box>
        </Box>
      ))}
    </Paper>
  );
}
