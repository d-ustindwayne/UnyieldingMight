import { useState } from 'react';
import { Accordion, AccordionDetails, AccordionSummary, Box, Stack, Typography } from '@mui/material';
import ExpandMoreRoundedIcon from '@mui/icons-material/ExpandMoreRounded';
import { problems } from '../data';
import { mono } from '../theme';
import Code from './Code';

export default function Problems() {
  const [open, setOpen] = useState(0);

  return (
    <Stack spacing={1.25}>
      {problems.map((p, i) => (
        <Accordion key={p.title} disableGutters expanded={open === i} onChange={(_, isOpen) => setOpen(isOpen ? i : -1)}>
          <AccordionSummary expandIcon={<ExpandMoreRoundedIcon />} sx={{ px: { xs: 2, sm: 2.5 } }}>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'baseline' }}>
              <Typography sx={{ fontFamily: mono, fontSize: '0.75rem', color: 'text.secondary' }}>
                {String(i + 1).padStart(2, '0')}
              </Typography>
              <Typography variant="h3">{p.title}</Typography>
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={{ px: { xs: 2, sm: 2.5 }, pb: 2.5 }}>
            <Typography color="text.secondary" sx={{ mb: 2, maxWidth: 680 }}>{p.context}</Typography>
            <Code code={p.code} file={p.file} />
          </AccordionDetails>
        </Accordion>
      ))}
    </Stack>
  );
}
