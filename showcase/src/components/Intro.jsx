import { useState } from 'react';
import { Box, Button, Chip, IconButton, Paper, Stack, Tooltip, Typography } from '@mui/material';
import GitHubIcon from '@mui/icons-material/GitHub';
import ContentCopyRoundedIcon from '@mui/icons-material/ContentCopyRounded';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import { REPO_URL, rewriteDiff } from '../data';
import { mono } from '../theme';

const cloneCmd = `git clone ${REPO_URL}.git`;

function CloneBox() {
  const [copied, setCopied] = useState(false);
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(cloneCmd);
      setCopied(true);
      setTimeout(() => setCopied(false), 1600);
    } catch {
      /* clipboard unavailable: the command stays selectable */
    }
  };
  return (
    <Box
      sx={{
        display: 'flex', alignItems: 'center', gap: 1, pl: 2, pr: 0.5, py: 0.5,
        borderRadius: 2, border: '1px solid rgba(255,255,255,0.1)', background: 'rgba(0,0,0,0.4)',
        fontFamily: mono, fontSize: { xs: '0.7rem', sm: '0.8rem' }, minWidth: 0,
      }}
    >
      <Box component="span" sx={{ color: 'primary.main' }}>$</Box>
      <Box component="span" sx={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', userSelect: 'all' }}>
        {cloneCmd}
      </Box>
      <Tooltip title={copied ? 'copied' : 'copy'}>
        <IconButton size="small" onClick={copy} aria-label="Copy clone command">
          {copied ? <CheckRoundedIcon fontSize="small" color="secondary" /> : <ContentCopyRoundedIcon fontSize="small" />}
        </IconButton>
      </Tooltip>
    </Box>
  );
}

export default function Intro() {
  return (
    <Box
      component="header"
      id="readme"
      sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', lg: '1.1fr 1fr' }, gap: 3, mb: { xs: 7, md: 10 }, scrollMarginTop: 88 }}
    >
      <Box sx={{ minWidth: 0 }}>
        <Stack direction="row" spacing={1} sx={{ mb: 2.5, flexWrap: 'wrap', rowGap: 1 }}>
          <Chip size="small" variant="outlined" label="portfolio · academic project" />
          <Chip size="small" variant="outlined" label="not deployed" />
        </Stack>
        <Typography variant="h1" component="h1" sx={{ mb: 1.5 }}>
          Unyielding<Box component="span" sx={{ color: 'primary.main' }}>Might</Box>
        </Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 520, mb: 3 }}>
          A gym class booking app that began as a group project and was rebuilt solo in Kotlin.
          This page covers the engineering, not the product.
        </Typography>
        <Stack spacing={1.5} sx={{ maxWidth: 520 }}>
          <CloneBox />
          <Button
            href={REPO_URL}
            target="_blank"
            rel="noopener"
            variant="contained"
            size="large"
            startIcon={<GitHubIcon />}
            sx={{ alignSelf: 'flex-start', boxShadow: '0 8px 30px -10px rgba(247,151,29,0.6)' }}
          >
            Read the source
          </Button>
        </Stack>
      </Box>

      <Paper sx={{ p: 0, overflow: 'hidden', minWidth: 0 }}>
        <Box sx={{ px: 2, py: 1, fontFamily: mono, fontSize: '0.72rem', color: 'text.secondary', borderBottom: '1px solid rgba(255,255,255,0.06)' }}>
          git diff original..rewrite --stat-ish
        </Box>
        <Box component="ul" sx={{ m: 0, p: 1, listStyle: 'none', fontFamily: mono, fontSize: { xs: '0.72rem', sm: '0.8rem' } }}>
          {rewriteDiff.map(([sign, text]) => (
            <Box
              component="li"
              key={text}
              sx={{
                display: 'flex', gap: 1.5, px: 1.5, py: 0.6, borderRadius: 1,
                color: sign === '+' ? '#BFE8CD' : '#E6A9A9',
                background: sign === '+' ? 'rgba(143,211,168,0.06)' : 'rgba(230,120,120,0.06)',
              }}
            >
              <span aria-hidden>{sign}</span>
              <span>{text}</span>
            </Box>
          ))}
        </Box>
      </Paper>
    </Box>
  );
}
