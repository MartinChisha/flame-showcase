import type { ReactNode } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { competitionApi } from '../api/competitionApi';
import { toUserMessage } from '../api/createApiClient';
import { seasonSchema, type SeasonFormValues } from './seasonSchema';

export function SeasonForm({ divisionId }: { divisionId: string }) {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SeasonFormValues>({
    resolver: zodResolver(seasonSchema),
    defaultValues: { fixtureGeneration: 'ROUND_ROBIN_DOUBLE' },
  });

  const createSeason = useMutation({
    mutationFn: (values: SeasonFormValues) =>
      competitionApi.createSeason({
        divisionId,
        name: values.name,
        seasonYear: values.seasonYear,
        startDate: values.startDate,
        endDate: values.endDate,
      }),
    onSuccess: (season) => queryClient.setQueryData(['season', season.id], season),
  });

  return (
    <form onSubmit={handleSubmit((values) => createSeason.mutateAsync(values))} noValidate>
      <Field label="Season name" error={errors.name?.message}>
        <input {...register('name')} placeholder="2027 Premier Season" />
      </Field>
      <Field label="Year" error={errors.seasonYear?.message}>
        <input type="number" {...register('seasonYear')} />
      </Field>
      <Field label="Starts" error={errors.startDate?.message}>
        <input type="date" {...register('startDate')} />
      </Field>
      <Field label="Ends" error={errors.endDate?.message}>
        <input type="date" {...register('endDate')} />
      </Field>
      <Field label="Format" error={errors.fixtureGeneration?.message}>
        <select {...register('fixtureGeneration')}>
          <option value="ROUND_ROBIN_SINGLE">Single round-robin</option>
          <option value="ROUND_ROBIN_DOUBLE">Double round-robin (home and away)</option>
        </select>
      </Field>

      {createSeason.isError && <p role="alert">{toUserMessage(createSeason.error)}</p>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Creating...' : 'Create season'}
      </button>
    </form>
  );
}

function Field({ label, error, children }: { label: string; error?: string | undefined; children: ReactNode }) {
  return (
    <label className="block space-y-1">
      <span className="text-sm font-medium">{label}</span>
      {children}
      {error && <span className="text-sm text-red-600">{error}</span>}
    </label>
  );
}
