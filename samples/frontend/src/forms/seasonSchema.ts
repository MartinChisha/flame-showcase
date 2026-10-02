import { z } from 'zod';

// The schema is the single source of truth: validation rules and the form's
// TypeScript type are both derived from it.
export const seasonSchema = z
  .object({
    name: z.string().trim().min(3, 'Give the season a name of at least 3 characters').max(80),
    seasonYear: z.coerce
      .number<number>()
      .int()
      .min(2000, 'Year looks too early')
      .max(2100, 'Year looks too late'),
    startDate: z.iso.date('Choose a start date'),
    endDate: z.iso.date('Choose an end date'),
    fixtureGeneration: z.enum(['ROUND_ROBIN_SINGLE', 'ROUND_ROBIN_DOUBLE']),
  })
  .refine((s) => s.endDate > s.startDate, {
    path: ['endDate'],
    message: 'The season must end after it starts',
  })
  .refine((s) => s.startDate.startsWith(String(s.seasonYear)), {
    path: ['startDate'],
    message: 'Start date should fall in the season year',
  });

export type SeasonFormValues = z.infer<typeof seasonSchema>;
